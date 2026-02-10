export interface UserAuth {
    uuid: string;
    username: string;
    fullName: string;
    photo: string;
    role: 'ADMIN' | 'GUIDE' | 'TOURIST' | 'VISITOR';
}

export interface User {
    uuid: string;
    firstName: string;
    lastName: string;
    photo: string;
    username: string;
    email: string;
    role: 'ADMIN' | 'GUIDE' | 'TOURIST' | 'VISITOR';
}

export interface UserDetails extends User {
    phone: string;
    status: string;
    lastLoginAt: string;
    oauthProviders: string[];
}