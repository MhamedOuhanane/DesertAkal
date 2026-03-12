import { Register } from '../auth/auth.models';
import { Language } from './language.model';
import { PageAble } from './response.models';
import { User, UserFind, UserUpdate } from './user.models';

export interface Guide extends User {
    readonly rating: number;
    readonly languages: Language[];
}

export interface GuideFilters extends PageAble{
    search?: string;
    language?: string;
}

export interface GuideFind extends UserFind {
    readonly experienceYears: number;
    readonly rating: number;
    readonly reviewCount: number;
    readonly languages: Language[];
}

export interface GuideCreate extends Register {
    experienceYears: number;
    phone: string;
    languageUsUuids: string[];
}

export interface GuideUpdate extends UserUpdate {
    experienceYears?: number;
    languageUsUuids?: string[];
}
