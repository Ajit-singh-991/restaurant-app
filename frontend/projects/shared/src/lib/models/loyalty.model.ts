export interface LoyaltyAccount {
  id: number;
  customerId: number;
  customerName: string;
  points: number;
  totalPointsEarned: number;
  tier: LoyaltyTier;
  pointsToNextTier: number;
  createdAt: string;
}

export interface LoyaltyTransaction {
  id: number;
  type: TransactionType;
  points: number;
  description: string;
  orderId: number | null;
  createdAt: string;
}

export type LoyaltyTier = 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM';
export type TransactionType = 'EARNED' | 'REDEEMED' | 'BONUS' | 'EXPIRED';
