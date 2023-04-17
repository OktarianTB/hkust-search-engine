import React, { ReactElement, useState } from "react";
import SearchBar from "../components/SearchBar";
import { CircularProgress, Typography } from "@mui/material";
import {
    createStyles,
    makeStyles,
    Theme,
} from '@material-ui/core';
import { search } from "../api/SearchApi";
import { SearchResult, SearchResults } from "../types/ResultType";
import ResultCard from "../components/ResultCard";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        text: {
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
        }
    })
);

interface ResultsProps {
    query: string;
    searchResults: SearchResult[];
}

const Results = ({ query, searchResults }: ResultsProps): ReactElement => {
    const classes = useStyles();

    return (
        <div>
            <div className={classes.text}>
                <Typography variant="caption">{searchResults.length === 50 ? "50+" : searchResults.length} results found for query '{query}'</Typography>
            </div>
            {searchResults.slice(0, 50).map((result: SearchResult) => <ResultCard key={result.url} searchResult={result} />)}
        </div>
    );
}

const Spinner = (): ReactElement => {
    const classes = useStyles();

    return (
        <div className={classes.text}>
            <CircularProgress />
        </div>
    );
}

const Search = (): ReactElement => {
    const classes = useStyles();

    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [query, setQuery] = useState<string>("");
    const [searchResults, setSearchResults] = useState<SearchResult[]>([]);

    const handleSearch = async (query: string): Promise<void> => {
        setSearchResults([]);
        setError(null);
        setIsLoading(true);

        try {
            const searchResults: SearchResult[] = await search(query);
            setSearchResults(Object.values(searchResults));
        } catch (error) {
            setError(`Error while searching for query '${query}'`);
            console.error(error);
        }

        setQuery(query);
        setIsLoading(false);
    };

    return (
        <div style={{ marginBottom: 100 }}>
            <SearchBar onSearch={handleSearch} isLoading={isLoading} />

            {isLoading && <Spinner />}

            {searchResults && searchResults.length === 0 && !isLoading && query && !error && (
                <div className={classes.text}>
                    <Typography variant="caption">No results found for query '{query}'</Typography>
                </div>
            )}

            {searchResults && searchResults.length === 0 && !isLoading && error && (
                <div className={classes.text}>
                    <Typography variant="caption">{error}</Typography>
                </div>
            )}

            {searchResults && searchResults.length > 0 ? (
                <Results query={query} searchResults={searchResults} />
            ) : <div></div>}
        </div>
    );
};

export default Search;