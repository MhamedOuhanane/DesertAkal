import { ReviewableType } from '../enums/reviewable-type.enum';
import { PageAble } from './response.models';

export interface Review {
    readonly uuid: string;
    readonly rating: number;
    readonly comment: string;
    readonly reviewableUuid: string;
    readonly reviewableType: ReviewableType;
    readonly touristUuid: string;
    readonly touristName: string;
    readonly touristPhoto: string;
    readonly createdAt: string | Date;
    readonly updatedAt: string | Date;
    readonly reviewableName: string;
}

export interface ReviewFilters extends PageAble {
    type?: string;
    minRating?: number;
}

export interface ReviewCreate {
    rating: string;
    comment: string;
    reviewableUuid: string;
    reviewableType: ReviewableType;
}

export interface ReviewUpdate {
    rating?: string;
    comment?: string;
}
