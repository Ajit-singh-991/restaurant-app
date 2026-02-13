export interface Campaign {
  id: number;
  name: string;
  description?: string;
  type: CampaignType;
  status: CampaignStatus;
  discountPercent?: number;
  discountAmount?: number;
  promoCode?: string;
  startDate?: string;
  endDate?: string;
  targetSegment?: string;
  usageCount: number;
  maxUsage?: number;
  createdBy?: string;
  createdAt: string;
}

export type CampaignType = 'PERCENTAGE_DISCOUNT' | 'FLAT_DISCOUNT' | 'BUY_ONE_GET_ONE' | 'FREE_DELIVERY' | 'HAPPY_HOUR';
export type CampaignStatus = 'DRAFT' | 'ACTIVE' | 'PAUSED' | 'EXPIRED' | 'CANCELLED';

export interface CreateCampaignRequest {
  name: string;
  description?: string;
  type: string;
  discountPercent?: number;
  discountAmount?: number;
  promoCode?: string;
  startDate?: string;
  endDate?: string;
  targetSegment?: string;
  maxUsage?: number;
}

export interface ValidatePromoResponse {
  valid: boolean;
  message: string;
  discountPercent?: number;
  discountAmount?: number;
  type?: string;
}

export interface CampaignStats {
  totalCampaigns: number;
  activeCampaigns: number;
  totalRedemptions: number;
  topCampaigns: Campaign[];
}
