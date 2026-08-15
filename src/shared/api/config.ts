const explicitApiUrl = import.meta.env.VITE_API_URL?.trim();
const explicitWsUrl = import.meta.env.VITE_WS_URL?.trim();

function resolveWsUrl() {
  if (explicitWsUrl) {
    return explicitWsUrl;
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${protocol}//${window.location.host}/ws`;
}

export const apiBaseUrl = explicitApiUrl || '/api';
export const websocketUrl = resolveWsUrl();
