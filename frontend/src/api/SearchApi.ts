import axios, { AxiosResponse } from 'axios';
import { SearchResult, SearchResults } from '../types/ResultType';

export const search = async (query: string): Promise<SearchResults> => {
    try {
        const response: AxiosResponse<{ results: SearchResult[], time: number }> = await axios.post(
            "http://localhost:8000/search",
            { query }
        );

        const data: SearchResults = {
            results: Object.values(response.data.results),
            time: response.data.time,
        };

        return data;
    } catch (error) {
        throw new Error("Failed to fetch search results");
    }
}