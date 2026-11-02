export async function request(path, options = {}) {
  const method = options.method ?? 'GET';
  let response;
  try {
    let csrfHeaders = {};
    if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
      const csrf = await request('/auth/csrf');
      csrfHeaders = { [csrf.headerName]: csrf.token };
    }
    response = await fetch(`/api${path}`, {
      ...options,
      credentials: 'same-origin',
      headers: {
        ...(options.body instanceof FormData
          ? {}
          : { 'Content-Type': 'application/json' }),
        ...csrfHeaders,
        ...options.headers,
      },
    });
  } catch (error) {
    if (error.name === 'AbortError' || error.status) throw error;
    throw new Error(
      'Não foi possível conectar. Confira sua conexão e tente novamente.',
    );
  }
  if (response.status === 204) return null;
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    if (response.status === 401 && !path.startsWith('/auth/'))
      window.dispatchEvent(new Event('sessao-expirada'));
    const fields = Object.values(data?.campos ?? {});
    const error = new Error(
      fields.length
        ? fields.join(' ')
        : (data?.mensagem ?? 'Não foi possível concluir. Tente novamente.'),
    );
    error.status = response.status;
    throw error;
  }
  return data;
}
const post = (path, data) =>
  request(path, { method: 'POST', body: JSON.stringify(data) });
export const api = {
  eu: (signal) => request('/auth/eu', { signal }),
  cadastrar: (dados) => post('/auth/cadastro', dados),
  entrar: (email, senha) =>
    request('/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ email, password: senha }).toString(),
    }),
  sair: () => post('/auth/logout', {}),
  listarGrupos: (signal) => request('/grupos', { signal }),
  criarGrupo: (grupo) => post('/grupos', grupo),
  consultarGrupo: (id, signal) => request(`/grupos/${id}`, { signal }),
  consultarSaldos: (id, signal) =>
    request(`/grupos/${id}/saldos`, { signal, cache: 'no-store' }),
  listarParticipantes: (id, signal) =>
    request(`/grupos/${id}/participantes`, { signal }),
  removerParticipante: (id, participanteId) =>
    request(`/grupos/${id}/participantes/${participanteId}`, {
      method: 'DELETE',
    }),
  cadastrarDespesa: (id, despesa) => post(`/grupos/${id}/despesas`, despesa),
  previaDivisao: (id, valorTotal, participanteIds) =>
    post(`/grupos/${id}/despesas/previa-divisao`, {
      valorTotal,
      participanteIds,
    }),
  listarDespesas: (grupoId, signal) =>
    request(`/grupos/${grupoId}/despesas`, { signal }),
  consultarDespesa: (grupoId, id) =>
    request(`/grupos/${grupoId}/despesas/${id}`),
  editarDespesa: (grupoId, id, versao, dados) =>
    request(`/grupos/${grupoId}/despesas/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ versao, dados }),
    }),
  excluirDespesa: (grupoId, id, versao) =>
    request(`/grupos/${grupoId}/despesas/${id}?versao=${versao}`, {
      method: 'DELETE',
    }),
  enviarComprovante: (grupoId, id, versao, arquivo) => {
    const body = new FormData();
    body.append('arquivo', arquivo);
    return request(
      `/grupos/${grupoId}/despesas/${id}/comprovante?versao=${versao}`,
      { method: 'POST', body },
    );
  },
  removerComprovante: (grupoId, id, versao) =>
    request(`/grupos/${grupoId}/despesas/${id}/comprovante?versao=${versao}`, {
      method: 'DELETE',
    }),
  urlComprovante: (grupoId, id, baixar = false) =>
    `/api/grupos/${grupoId}/despesas/${id}/comprovante?baixar=${baixar}`,
  convidar: (id, email) => post(`/grupos/${id}/convites`, { email }),
  convitesEnviados: (id, signal) =>
    request(`/grupos/${id}/convites`, { signal }),
  convitesRecebidos: (signal) => request('/convites', { signal }),
  responderConvite: (id, aceitar) =>
    post(`/convites/${id}/resposta`, { aceitar }),
};
