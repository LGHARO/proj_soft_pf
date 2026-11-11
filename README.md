# Sistema de Avaliações (Auth0 + Spring Boot + React + AWS + Vercel)

API e Front-end de avaliações corporativas autenticados pelo **Auth0**, com controle de permissões por roles (`ADMIN` e `USER`), deploy automatizado do Back-end na **AWS EC2** via **GitHub Actions** e deploy do Front-end na **Vercel**.

---

## 1. Estrutura do Projeto

- [`backend/`](./backend): API REST em **Spring Boot (Java 21)** + **OAuth2 Resource Server (Auth0)** + **JPA (PostgreSQL / H2)**.
  - `POST /avaliacoes` — Cadastra uma nova avaliação (**apenas `ADMIN`**).
  - `GET /avaliacoes` — Lista todas as avaliações (**`ADMIN` e `USER`**).
- [`frontend/`](./frontend): Aplicação **React + Vite** integrada com `@auth0/auth0-react`.
- [`docker-compose.yml`](./docker-compose.yml): Sobe o banco PostgreSQL (`avaliacoes-db`) e a API (`avaliacoes-api`) na porta `8080`.
- [`.github/workflows/deploy.yml`](./.github/workflows/deploy.yml): Pipeline CI/CD do GitHub Actions que roda os testes automatizados e faz deploy via Docker Compose no runner `self-hosted` da AWS EC2.

---

## 2. Credenciais Auth0 Já Configuradas no Código

- **Domain:** `dev-mq0u4hp5ecfo3ppq.us.auth0.com`
- **Client ID:** `t9s0DHtQgkhC2yIBPVA4Wvw1E8SdpIxD`
- **Audience padrão:** `https://dev-mq0u4hp5ecfo3ppq.us.auth0.com/api/v2/`

### O que você precisa conferir no painel do Auth0:
1. **Em Applications > Applications > (Sua App SPA):**
   - **Allowed Callback URLs:** `http://localhost:5173, https://SEU-PROJETO.vercel.app`
   - **Allowed Logout URLs:** `http://localhost:5173, https://SEU-PROJETO.vercel.app`
   - **Allowed Web Origins:** `http://localhost:5173, https://SEU-PROJETO.vercel.app`
2. **Em User Management > Roles:**
   - Crie as roles `ADMIN` e `USER`.
   - Em **User Management > Users**, atribua a role `ADMIN` ao usuário administrador e `USER` ao usuário comum.
3. **Em Actions > Library > Create Action > Build from scratch** (Trigger: `Login / Post Login`):
   Cole o código abaixo, clique em **Deploy** e adicione a Action ao fluxo em **Actions > Flows > Login**:
   ```javascript
   exports.onExecutePostLogin = async (event, api) => {
     const namespace = 'https://musica-insper.com';
     if (event.authorization) {
       api.idToken.setCustomClaim(`${namespace}/roles`, event.authorization.roles);
       api.accessToken.setCustomClaim(`${namespace}/roles`, event.authorization.roles);
     }
     if (event.user && event.user.email) {
       api.accessToken.setCustomClaim(`${namespace}/email`, event.user.email);
     }
   };
   ```

---

## 3. Passo a Passo: Deploy do Back-End na AWS (com GitHub Actions)

1. **Na instância EC2 da AWS:**
   - No **Security Group** da EC2, libere a porta **`8080`** (Custom TCP `8080`, origem `0.0.0.0/0`) nas *Inbound Rules*.
   - Conecte via SSH na EC2 e instale o Docker:
     ```bash
     sudo apt update
     sudo apt install -y docker.io docker-compose-v2
     sudo usermod -aG docker $USER
     newgrp docker
     ```
2. **Configurar o Self-Hosted Runner do GitHub Actions na EC2:**
   - No seu repositório do GitHub, vá em **Settings > Actions > Runners > New self-hosted runner** (Linux / x64).
   - Copie e execute os comandos mostrados pelo GitHub na sua máquina EC2.
   - Para deixá-lo rodando em background:
     ```bash
     sudo ./svc.sh install
     sudo ./svc.sh start
     ```
3. **Subir o código para o GitHub:**
   - Assim que você der `git push origin main`, a Action [`.github/workflows/deploy.yml`](./.github/workflows/deploy.yml) vai rodar os testes e executar `docker compose up -d --build` automaticamente na sua EC2!

---

## 4. Passo a Passo: Deploy do Front-End na Vercel

1. **Apontar o IP da sua EC2 no [`frontend/vercel.json`](./frontend/vercel.json):**
   - Edite o arquivo `frontend/vercel.json` substituindo `SEU_IP_PUBLICO_EC2` pelo IP público da sua máquina AWS (ex.: `http://54.233.10.20:8080/:path*`).
   - *Por que isso é importante?* A Vercel usa `HTTPS` e o IP da EC2 usa `HTTP`. Esse rewrite na Vercel faz o proxy reverso automático (`/api/avaliacoes` -> `http://IP_EC2:8080/avaliacoes`), evitando erro de *Mixed Content* no navegador!
2. **Importar na Vercel:**
   - Acesse [vercel.com](https://vercel.com/), clique em **Add New > Project** e selecione o seu repositório do GitHub.
   - Em **Root Directory**, clique em *Edit* e selecione a pasta **`frontend`**.
   - Clique em **Deploy**.
3. **Atualizar URL no Auth0:**
   - Copie a URL gerada pela Vercel (ex.: `https://simulado-proj-soft-pf.vercel.app`) e adicione em **Allowed Callback URLs**, **Allowed Logout URLs** e **Allowed Web Origins** na sua Application do Auth0.
