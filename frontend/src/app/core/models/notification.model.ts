export interface Notification {
    readonly uuid: string;
    readonly title: string;
    readonly date: string | Date;
    readonly seen: boolean;
}
