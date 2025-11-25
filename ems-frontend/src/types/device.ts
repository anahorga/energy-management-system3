export type DeviceDto = {
    id: number;
    name: string;
    consumption: number;
    user?: { id: number };
};
