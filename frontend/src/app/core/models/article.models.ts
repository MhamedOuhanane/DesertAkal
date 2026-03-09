export interface Article {
    readonly uuid: string;
    readonly content: string;
    readonly coverImage: string;
    readonly commentCount: number;
    readonly reactionCount: number;
    readonly createdAt: string | Date;
    readonly updatedAt: string | Date;
    readonly userUuid: string;
    readonly userName: string;
    readonly userPhoto: string;
}

export interface ArticleCreate {
    content: string;
}

export interface ArticleUpdate {
    content: string;
}