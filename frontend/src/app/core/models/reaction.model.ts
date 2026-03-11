import { ReactionType } from '../enums/reaction.enum';

export type Action = 'ADDED' | 'CHANGED' | 'REMOVED';

export interface Reaction {
    readonly uuid: string;
    readonly reaction: ReactionType;
    readonly emoji: string;
    readonly createdAt: string | Date;
    readonly articleUuid: string;
    readonly userUuid: string;
    readonly userName: string;
    readonly userPhoto: string;
}

export interface ReactionSummary {
    readonly totalCount: number;
    readonly countByType: Record<ReactionType, number>;
    readonly userReaction: ReactionType;
    readonly articleUuid: string;
}

export interface ReactionCreate {
    reaction: ReactionType;
    articleUuid: string;
}

export interface ReactionToggleResponse {
    readonly action: Action;
    readonly userReaction: ReactionType;
    readonly totalCount: number;
    readonly countByType: Record<ReactionType, number>;
    readonly articleUuid: string;
}
