export interface MonthlyStats {
    readonly month: string;
    readonly revenue: number;
    readonly reservationCount: number;
}

export interface AdminDashboard {
    readonly totalUsers: number;
    readonly totalTours: number;
    readonly totalReservations: number;
    readonly totalRevenue: number;
    readonly averageTourRating: number;
    readonly totalArticles: number;
    readonly activeGuides: number;
    readonly reservationsByStatus: Record<string, number>;
    readonly monthlyPerformance: MonthlyStats[];
}