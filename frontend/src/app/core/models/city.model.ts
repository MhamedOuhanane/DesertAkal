import { Image } from "./image.model";

export interface City {
    readonly uuid: string;
    readonly name: string;
    readonly map_lat: number;
    readonly map_lng: number;
}

export interface CityFind extends City {
    readonly description: string;
    readonly createdAt: string | Date;
    readonly updatedAt: string | Date;
    readonly images: Image[];
}

export interface CityCreate {
    name: string;
    map_lat: number;
    map_lng: number;
    description: string;
}

export interface CityUpdate {
    name?: string;
    map_lat?: number;
    map_lng?: number;
    description?: string;
}