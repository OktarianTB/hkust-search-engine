import axios, { AxiosResponse } from 'axios';
import { SearchResult, SearchResults } from '../types/ResultType';

export const search = async (query: string): Promise<SearchResult[]> => {
    try {
        const response: AxiosResponse<{ results: SearchResult[] }> = await axios.post(
            "http://localhost:8000/search",
            { query }
        );
        return response.data.results;
    } catch (error) {
        throw new Error("Failed to fetch search results");
    }
}