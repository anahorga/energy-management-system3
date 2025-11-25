export type JwtClaims = { username?: string; role?: string; id?: number | string };

export function decodeJwt(token: string): JwtClaims | null {
    try {
        const payload = token.split(".")[1];
        const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
        return JSON.parse(decodeURIComponent(escape(json)));
    } catch {
        return null;
    }
}
export function getUserIdFromToken(): number | null {
    const token = localStorage.getItem("token");
    if (!token) return null;
    try {
        const payload = token.split(".")[1];
        const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
        const claims = JSON.parse(decodeURIComponent(escape(json)));
        // backend-ul pune claim-urile username/role/id în token, ex. JwtTokenService.CLAIM_ID = "id"
        // returnează Long -> front-ul îl tratează ca number
        return claims?.id != null ? Number(claims.id) : null;
    } catch {
        return null;
    }
}
