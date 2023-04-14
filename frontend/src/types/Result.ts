export interface WordFrequency {
    word: string;
    frequency: number;
}

export interface Result {
    title: string;
    score: number;
    url: string;
    size: string;
    lastModified: string;
    topWordFrequencies: WordFrequency[];
    parentLinks: string[];
    childLinks: string[];
};