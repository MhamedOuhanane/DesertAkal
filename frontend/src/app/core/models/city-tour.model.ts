import { CityFind } from "./city.model";

export interface CityTour {
    readonly orderIndex: number;
    readonly daysCount: number;
    readonly cityUuid: string;
    readonly cityName: string;
}

export interface CityTourFind {
    readonly orderIndex: number;
    readonly daysCount: number;
    readonly city: CityFind;
    readonly tourUuid: string;
}

export interface CityTourCreate {
    orderIndex: number;
    daysCount: number;
    cityUuid: string;
}