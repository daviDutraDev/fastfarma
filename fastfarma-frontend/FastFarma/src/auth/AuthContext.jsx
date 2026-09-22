import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { apiPost } from "../services/api/httpClient.js";

const STORAGE_KEY = "fastfarma:auth";

const AuthContext = createContext(null);

const readStored = () => {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (!raw) return null;
        const parsed = JSON.parse(raw);
        // expira em segundos; se passou, descarta
        if (parsed?.expiresAt && parsed.expiresAt < Date.now()) {
            localStorage.removeItem(STORAGE_KEY);
            return null;
        }
        return parsed;
    } catch {
        return null;
    }
};

const persist = (value) => {
    if (value) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(value));
    } else {
        localStorage.removeItem(STORAGE_KEY);
    }
};

export function AuthProvider({ children }) {
    const [auth, setAuth] = useState(() => readStored());

    // Sincroniza entre abas/janelas
    useEffect(() => {
        const onStorage = (e) => {
            if (e.key === STORAGE_KEY) {
                setAuth(readStored());
            }
        };
        window.addEventListener("storage", onStorage);
        return () => window.removeEventListener("storage", onStorage);
    }, []);

    const login = useCallback(async (email, senha) => {
        const resp = await apiPost("/api/auth/login", { email, senha });
        const dados = resp?.dados;
        if (!dados?.token) {
            throw new Error("Resposta inválida do servidor (sem token).");
        }
        const value = {
            token: dados.token,
            tokenType: dados.tokenType || "Bearer",
            user: {
                id: dados.id,
                nome: dados.nome,
                email: dados.email,
                tipo: dados.tipo,
                telefone: dados.telefone,
            },
            expiresAt: Date.now() + (dados.expiresInSeconds ?? 0) * 1000,
        };
        persist(value);
        setAuth(value);
        return value;
    }, []);

    const logout = useCallback(() => {
        persist(null);
        setAuth(null);
    }, []);

    const value = useMemo(() => ({
        auth,
        token: auth?.token ?? null,
        user: auth?.user ?? null,
        isAuthenticated: Boolean(auth?.token),
        isFuncionario: auth?.user?.tipo === "FUNCIONARIO",
        login,
        logout,
    }), [auth, login, logout]);

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => {
    const ctx = useContext(AuthContext);
    if (!ctx) {
        throw new Error("useAuth precisa estar dentro de <AuthProvider>");
    }
    return ctx;
};
