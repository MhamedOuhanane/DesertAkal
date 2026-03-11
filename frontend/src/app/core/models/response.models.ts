export interface Pagination<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    isFirst: boolean;
    isLast: boolean;
}

export interface ApiResponse<T> {
    timestamp: string;
    message: string;
    status: number;
    path: string;
    data: T | null;
}

export interface PageAble {
    page?: number;
    size?: number;
    sortBy?: string;
    order?: string;
}
