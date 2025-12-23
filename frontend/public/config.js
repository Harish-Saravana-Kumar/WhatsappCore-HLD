// Configuration - Automatically detects environment
const CONFIG = {
    // Detects environment and uses appropriate URLs
    API_URL: (() => {
        if (window.location.hostname === 'localhost') {
            return 'http://localhost:8080/api';
        }
        // Use environment variable if available (Vercel), otherwise use default Render URL
        return window.ENV_BACKEND_URL || 'https://whatsapp-backend-paww.onrender.com/api';
    })(),
    
    WS_URL: (() => {
        if (window.location.hostname === 'localhost') {
            return 'ws://localhost:8081';
        }
        // Use environment variable if available (Vercel), otherwise use default Render URL
        const backendUrl = window.ENV_BACKEND_URL || 'https://whatsapp-backend-paww.onrender.com';
        return backendUrl.replace('https://', 'wss://').replace('/api', '');
    })()
};

console.log('✓ WhatsApp Frontend loaded');
console.log('API URL:', CONFIG.API_URL);
console.log('WebSocket URL:', CONFIG.WS_URL);
