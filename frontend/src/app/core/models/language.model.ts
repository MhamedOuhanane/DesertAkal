export interface Language {
    readonly uuid: string;
    readonly name: string;
    readonly code: string;
}

export interface LanguageCreate {
    name: string;
    code: string;
}

export interface LanguageUpdate {
    name?: string;
    code?: string;
}
