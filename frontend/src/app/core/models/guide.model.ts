import { Language } from './language.model';
import { User, UserFind, UserUpdate } from './user.models';

export interface Guide extends User {
    readonly rating: number;
    readonly languages: Language[];
}

export interface GuideFind extends UserFind {
    readonly experienceYears: number;
    readonly rating: number;
    readonly reviewCount: number;
    readonly languages: Language[];
}

export interface GuideCreate {
    experienceYears: number;
    phone: string;
    languageUsUuids: string[];
}

export interface GuideUpdate extends UserUpdate {
    experienceYears?: number;
    languageUsUuids?: string[];
}
