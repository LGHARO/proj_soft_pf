import React from 'react'
import ReactDOM from 'react-dom/client'
import { Auth0Provider } from '@auth0/auth0-react'
import App from './App.jsx'
import './App.css'

const domain = import.meta.env.VITE_AUTH0_DOMAIN || 'dev-mq0u4hp5ecfo3ppq.us.auth0.com'
const clientId = import.meta.env.VITE_AUTH0_CLIENT_ID || 't9s0DHtQgkhC2yIBPVA4Wvw1E8SdpIxD'
const audience = import.meta.env.VITE_AUTH0_AUDIENCE || `https://${domain}/api/v2/`

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Auth0Provider
      domain={domain}
      clientId={clientId}
      authorizationParams={{
        redirect_uri: window.location.origin,
        audience: audience
      }}
    >
      <App />
    </Auth0Provider>
  </React.StrictMode>
)
