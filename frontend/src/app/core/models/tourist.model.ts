import { UserFind, UserUpdate } from './user.models';

export interface Tourist extends UserFind {
    readonly avatarUrl: string;
    readonly nationality: string;
    readonly language: string;
}

export interface TouristUpdate extends UserUpdate {
    nationality?: string;
    language?: string;
}
