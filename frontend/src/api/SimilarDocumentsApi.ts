import axios, { AxiosResponse } from 'axios';
import { SearchResult, SearchResults } from '../types/ResultType';

export const getSimilarDocuments = async (docId: number): Promise<SearchResults> => {
    try {
        const response: AxiosResponse<{ results: SearchResult[], time: number }> = await axios.post(
            "http://localhost:8000/similar-documents",
            { docId }
        );

        const data: SearchResults = {
            results: Object.values(response.data.results),
            time: response.data.time,
        };

        return data;
    } catch (error) {
        throw new Error("Failed to fetch similar documents");
    }
}