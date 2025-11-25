import { api } from "./api";
import type { RegisterAdminRequest, AuthUserDto } from "../types/auth";
import type { UserServiceDto } from "../types/user";


async function authRegisterAdmin(payload: RegisterAdminRequest): Promise<AuthUserDto> {
    const { data } = await api.post<AuthUserDto>("/api/auth/register-admin", payload);
    return data;
}

export async function createUserAsAdmin(
    authReq: { username: string; password: string; userRole: "USER" | "ADMIN" },
    profile: Omit<UserServiceDto, "id">
): Promise<{ id: number; role: "ADMIN" | "USER" }> {

    const fullPayload: RegisterAdminRequest = {
        ...authReq,
        ...profile
    };

    const created = await authRegisterAdmin(fullPayload);

    return { id: created.id, role: created.userRole };
}