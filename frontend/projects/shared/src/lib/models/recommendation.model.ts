export interface RecommendedItem {
  itemId: number;
  itemName: string;
  category?: string;
  price?: number;
  score: number;
  reason: string;
}

export interface PersonalizedRecommendations {
  recommendations: RecommendedItem[];
  strategy: string;
}

export interface TrendingItems {
  items: RecommendedItem[];
  period: string;
}

export interface PairingSuggestion {
  itemId: number;
  itemName: string;
  pairsWith: RecommendedItem[];
}
