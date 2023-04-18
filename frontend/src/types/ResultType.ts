export interface WordFrequency {
    word: string;
    frequency: number;
}

export interface SearchResults {
    results: SearchResult[];
    time: number;
}

export interface SearchResult {
    docId: number;
    score: number;
    url: string;
    properties: {
        title: string;
        size: number;
        lastModifiedAt: string;
    };
    wordFrequencyMap: Record<string, number>;
    parentLinks: string[];
    childLinks: string[];
}