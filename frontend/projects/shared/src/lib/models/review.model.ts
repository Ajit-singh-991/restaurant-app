export interface Review {
  id: number;
  customerId: number;
  customerName: string;
  orderId: number;
  menuItemId: number | null;
  menuItemName: string | null;
  rating: number;
  comment: string;
  managementResponse: string | null;
  respondedAt: string | null;
  createdAt: string;
}

export interface CreateReviewRequest {
  orderId: number;
  menuItemId?: number;
  rating: number;
  comment?: string;
}

export interface ItemRatingSummary {
  menuItemId: number;
  menuItemName: string;
  averageRating: number;
  totalReviews: number;
}

export interface ReviewStats {
  overallRating: number;
  totalReviews: number;
  unansweredCount: number;
  ratingBreakdown: RatingBreakdown[];
}

export interface RatingBreakdown {
  rating: number;
  count: number;
  percentage: number;
}
