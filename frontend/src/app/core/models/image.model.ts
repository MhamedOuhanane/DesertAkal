export interface Image {
    uuid: string;
    image: string;
    isCover: boolean;
    createdAt: string | Date;
    updatedAt: string | Date;
    cityUuid: string;
}