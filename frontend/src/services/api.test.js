import test from 'node:test';
import assert from 'node:assert/strict';
import { api, request } from './api.js';

test('consulta saldos com sessão, cancelamento e sem cache ou mutações', async (t) => {
  const calls = [];
  const signal = new AbortController().signal;
  const data = {
    completo: true,
    despesasPendentes: [],
    participantes: [{ saldo: '-50.01' }],
  };
  t.mock.method(globalThis, 'fetch', async (url, options) => {
    calls.push({ url, options });
    return resposta(200, data);
  });
  assert.deepEqual(await api.consultarSaldos(7, signal), data);
  assert.equal(calls.length, 1);
  assert.equal(calls[0].url, '/api/grupos/7/saldos');
  assert.equal(calls[0].options.credentials, 'same-origin');
  assert.equal(calls[0].options.cache, 'no-store');
  assert.equal(calls[0].options.signal, signal);
  assert.equal(calls[0].options.body, undefined);
});

function resposta(status, data) {
  return { status, ok: status >= 200 && status < 300, json: async () => data };
}

test('envio busca CSRF novo e mantém sessão apenas no cookie', async (t) => {
  const calls = [];
  let token = 0;
  t.mock.method(globalThis, 'fetch', async (url, options) => {
    calls.push({ url, options });
    return url.endsWith('/csrf')
      ? resposta(200, { headerName: 'X-CSRF-TOKEN', token: `token-${++token}` })
      : resposta(204, null);
  });
  await api.entrar('ana@example.test', 'senha de teste longa');
  await api.sair();
  assert.deepEqual(
    calls.map((c) => c.url),
    ['/api/auth/csrf', '/api/auth/login', '/api/auth/csrf', '/api/auth/logout'],
  );
  assert.equal(calls[1].options.headers['X-CSRF-TOKEN'], 'token-1');
  assert.equal(calls[3].options.headers['X-CSRF-TOKEN'], 'token-2');
  assert.equal(calls[1].options.credentials, 'same-origin');
  assert.equal(
    new URLSearchParams(calls[1].options.body).get('email'),
    'ana@example.test',
  );
});

test('consulta privada não envia mutação nem busca CSRF', async (t) => {
  const calls = [];
  t.mock.method(globalThis, 'fetch', async (url) => {
    calls.push(url);
    return resposta(200, []);
  });
  assert.deepEqual(await api.listarGrupos(), []);
  assert.deepEqual(calls, ['/api/grupos']);
});

test('401 privado avisa expiração; consulta de conta não dispara esse evento', async (t) => {
  const events = [];
  const originalWindow = globalThis.window;
  globalThis.window = { dispatchEvent: (event) => events.push(event.type) };
  t.after(() => {
    if (originalWindow === undefined) delete globalThis.window;
    else globalThis.window = originalWindow;
  });
  t.mock.method(globalThis, 'fetch', async () =>
    resposta(401, { mensagem: 'Entre novamente.' }),
  );
  await assert.rejects(api.listarGrupos(), (error) => error.status === 401);
  await assert.rejects(api.eu(), (error) => error.status === 401);
  assert.deepEqual(events, ['sessao-expirada']);
});

test('falha ao obter CSRF não envia a operação', async (t) => {
  const calls = [];
  t.mock.method(globalThis, 'fetch', async (url) => {
    calls.push(url);
    return resposta(503, { mensagem: 'Indisponível.' });
  });
  await assert.rejects(api.criarGrupo({ nome: 'Teste' }), /Indisponível/);
  assert.deepEqual(calls, ['/api/auth/csrf']);
});

test('cancelamento de consulta preserva AbortError', async (t) => {
  t.mock.method(globalThis, 'fetch', async () => {
    throw new DOMException('Cancelado', 'AbortError');
  });
  await assert.rejects(
    request('/grupos'),
    (error) => error.name === 'AbortError',
  );
});

test('correção e exclusão enviam a versão lida com CSRF', async (t) => {
  const calls = [];
  t.mock.method(globalThis, 'fetch', async (url, options) => {
    calls.push({ url, options });
    return url.endsWith('/csrf')
      ? resposta(200, { headerName: 'X-CSRF-TOKEN', token: 'csrf-teste' })
      : resposta(204, null);
  });
  const dados = {
    descricao: 'Corrigida',
    valorTotal: '0.01',
    pagadorId: 8,
    participanteIds: [9],
    data: '2026-10-17',
  };
  await api.editarDespesa(1, 2, 3, dados);
  await api.excluirDespesa(1, 2, 4);
  assert.equal(calls[1].options.method, 'PUT');
  assert.deepEqual(JSON.parse(calls[1].options.body), { versao: 3, dados });
  assert.equal(calls[3].url, '/api/grupos/1/despesas/2?versao=4');
  assert.equal(calls[3].options.method, 'DELETE');
  assert.equal(calls[1].options.headers['X-CSRF-TOKEN'], 'csrf-teste');
  assert.equal(calls[3].options.headers['X-CSRF-TOKEN'], 'csrf-teste');
});

test('conflito de edição é informado sem repetir a gravação', async (t) => {
  let mutacoes = 0;
  t.mock.method(globalThis, 'fetch', async (url) => {
    if (url.endsWith('/csrf'))
      return resposta(200, { headerName: 'X-CSRF-TOKEN', token: 'token' });
    mutacoes++;
    return resposta(409, { mensagem: 'Despesa alterada. Recarregue.' });
  });
  await assert.rejects(
    api.editarDespesa(1, 2, 0, {}),
    (e) => e.status === 409 && /Recarregue/.test(e.message),
  );
  assert.equal(mutacoes, 1);
});

test('comprovante usa multipart sem forçar boundary, com CSRF e versão', async (t) => {
  const calls = [];
  t.mock.method(globalThis, 'fetch', async (url, options) => {
    calls.push({ url, options });
    return url.endsWith('/csrf')
      ? resposta(200, { headerName: 'X-CSRF-TOKEN', token: 'upload-token' })
      : resposta(200, { id: 2 });
  });
  const file = new File(['test'], 'nota.pdf', { type: 'application/pdf' });
  await api.enviarComprovante(1, 2, 3, file);
  assert.equal(calls[1].url, '/api/grupos/1/despesas/2/comprovante?versao=3');
  assert.equal(calls[1].options.headers['Content-Type'], undefined);
  assert.equal(calls[1].options.headers['X-CSRF-TOKEN'], 'upload-token');
  assert.equal(calls[1].options.body.get('arquivo').name, 'nota.pdf');
  await api.removerComprovante(1, 2, 4);
  assert.equal(calls[3].options.method, 'DELETE');
  assert.equal(calls[3].url, '/api/grupos/1/despesas/2/comprovante?versao=4');
});

test('prévia envia valor decimal e participantes sem cadastrar a despesa', async (t) => {
  const calls = [];
  t.mock.method(globalThis, 'fetch', async (url, options) => {
    calls.push({ url, options });
    return url.endsWith('/csrf')
      ? resposta(200, { headerName: 'X-CSRF-TOKEN', token: 'token' })
      : resposta(200, []);
  });
  await api.previaDivisao(7, '100.00', [3, 1, 2]);
  assert.equal(calls.length, 2);
  assert.equal(calls[1].url, '/api/grupos/7/despesas/previa-divisao');
  assert.deepEqual(JSON.parse(calls[1].options.body), {
    valorTotal: '100.00',
    participanteIds: [3, 1, 2],
  });
});
