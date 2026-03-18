export interface Notification {
    readonly uuid: string;
    readonly title: string;
    readonly date: string | Date;
    readonly seen: boolean;
}

export interface NotificationFind extends Notification {
    readonly message: string;
    readonly userUuid: string;
    readonly userName: string;
}
