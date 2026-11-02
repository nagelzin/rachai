import { useState } from 'react';
import { api } from '../services/api.js';
import Feedback from '../components/Feedback.jsx';

export default function Conta({ onEntrar, aviso }) {
  const [cadastro, setCadastro] = useState(false);
  const [visivel, setVisivel] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  async function enviar(event) {
    event.preventDefault();
    if (busy) return;
    const form = new FormData(event.currentTarget);
    const email = form.get('email').trim();
    const senha = form.get('senha');
    const nome = form.get('nome')?.trim();
    if (cadastro && !nome) {
      setError('Informe seu nome.');
      return;
    }
    setBusy(true);
    setError('');
    let contaCriada = false;
    try {
      if (cadastro) {
        await api.cadastrar({ nome, email, senha });
        contaCriada = true;
      }
      await api.entrar(email, senha);
      onEntrar(await api.eu());
    } catch (err) {
      setError(
        contaCriada
          ? `Conta criada. Entre para continuar. ${err.message}`
          : err.message,
      );
      if (contaCriada) setCadastro(false);
    } finally {
      setBusy(false);
    }
  }
  return (
    <main className="home content">
      <section className="intro">
        <span className="eyebrow">BONS ENCONTROS COMEÇAM AQUI</span>
        <h1>
          A turma reunida.
          <br />
          <em>As contas no lugar.</em>
        </h1>
        <p>
          Crie um grupo, convide seus amigos e registre os gastos do encontro em
          um espaço só de vocês.
        </p>
        <div className="steps">
          <span>Crie sua conta</span>
          <span>Reúna o grupo por convite</span>
          <span>Registre cada gasto</span>
        </div>
      </section>
      <section className="card auth-card">
        <span className="eyebrow">SEU ESPAÇO NO RACHAÍ</span>
        <h2>{cadastro ? 'Crie sua conta' : 'Que bom ter você aqui'}</h2>
        <p>
          {cadastro
            ? 'Um cadastro para todos os seus encontros.'
            : 'Entre para acessar seus grupos e convites.'}
        </p>
        {aviso && (
          <p role="status" className="feedback">
            {aviso}
          </p>
        )}
        <form onSubmit={enviar} aria-busy={busy}>
          {cadastro && (
            <>
              <label htmlFor="conta-nome">Seu nome</label>
              <input
                id="conta-nome"
                name="nome"
                autoComplete="name"
                required
                maxLength={120}
                disabled={busy}
              />
            </>
          )}
          <label htmlFor="conta-email">E-mail</label>
          <input
            id="conta-email"
            name="email"
            type="email"
            autoComplete="email"
            required
            maxLength={254}
            disabled={busy}
          />
          <label htmlFor="conta-senha">Senha</label>
          <div className="password-field">
            <input
              id="conta-senha"
              name="senha"
              type={visivel ? 'text' : 'password'}
              autoComplete={cadastro ? 'new-password' : 'current-password'}
              required
              minLength={cadastro ? 15 : undefined}
              maxLength={128}
              disabled={busy}
              aria-describedby={cadastro ? 'senha-ajuda' : undefined}
            />
            <button
              className="text-button"
              type="button"
              onClick={() => setVisivel(!visivel)}
              aria-label={visivel ? 'Ocultar senha' : 'Mostrar senha'}
            >
              {visivel ? 'Ocultar' : 'Mostrar'}
            </button>
          </div>
          {cadastro && (
            <small id="senha-ajuda">
              Use de 15 a 128 caracteres. Uma frase longa é uma boa opção.
            </small>
          )}
          <Feedback error={error} />
          <button className="primary" disabled={busy}>
            {busy ? 'Aguarde…' : cadastro ? 'Criar conta' : 'Entrar'}
          </button>
        </form>
        <p className="auth-switch">
          {cadastro ? 'Já tem uma conta?' : 'Primeira vez por aqui?'}{' '}
          <button
            type="button"
            className="text-button"
            disabled={busy}
            onClick={() => {
              setCadastro(!cadastro);
              setError('');
              setVisivel(false);
            }}
          >
            {cadastro ? 'Entrar na minha conta' : 'Criar uma conta'}
          </button>
        </p>
      </section>
    </main>
  );
}
