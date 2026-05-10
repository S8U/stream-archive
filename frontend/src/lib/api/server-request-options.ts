import { headers } from "next/headers";
import type { AxiosRequestConfig } from "axios";

export async function getServerRequestOptions(): Promise<AxiosRequestConfig | undefined> {
    const cookie = (await headers()).get("cookie");

    if (!cookie) {
        return undefined;
    }

    return {
        headers: {
            cookie,
        },
    };
}
