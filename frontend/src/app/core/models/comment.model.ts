export interface Comment {
    readonly uuid: string;
    readonly content: string;
    readonly createdAt: string | Date;
    readonly articleUuid: string;
    readonly userUuid: string;
    readonly userName: string;
    readonly userPhoto: string;
}

export interface CommentCreate {
    content: string;
    articleUuid: string;
}

export interface CommentUpdate {
    content: string;
}
