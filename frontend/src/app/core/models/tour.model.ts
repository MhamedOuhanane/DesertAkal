import { CityTour, CityTourCreate } from './city-tour.model';

export interface Tour {
    readonly uuid: string;
    readonly title: string;
    readonly image: string;
    readonly durationDays: number;
    readonly rating: number;
    readonly reviewCount: number;
    readonly cityTours: CityTour;
}

export interface TourFind {
    readonly uuid: string;
    readonly title: string;
    readonly image: string;
    readonly description: string;
    readonly durationDays: number;
    readonly rating: number;
    readonly reviewCount: number;
    readonly cityTours: CityTour;
    readonly createdAt: string | Date;
    readonly updatedAt: string | Date;
}

export interface TourCreate {
    title: string;
    description: string;
    cityTours: CityTourCreate[];
}

export interface TourUpdate {
    title?: string;
    description?: string;
    cityTours?: CityTourCreate[];
}
