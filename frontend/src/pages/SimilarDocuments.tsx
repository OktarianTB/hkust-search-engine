import React, { ReactElement, useEffect, useState } from "react";
import SearchBar from "../components/SearchBar";
import { CircularProgress, Link, Typography } from "@mui/material";
import {
    createStyles,
    makeStyles,
    Theme,
} from '@material-ui/core';
import { SearchResult, SearchResults } from "../types/ResultType";
import ResultCard from "../components/ResultCard";
import { useParams } from "react-router-dom";
import { getSimilarDocuments } from "../api/SimilarDocumentsApi";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        back: {
            marginBottom: 30,
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
        },
        text: {
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
        }
    })
);

interface ResultsProps {
    docId: string;
    searchResults: SearchResult[];
    time: number;
}

const Results = ({ docId, searchResults, time }: ResultsProps): ReactElement => {
    const classes = useStyles();

    return (
        <div>
            <div className={classes.text}>
                <Typography variant="caption">Top {searchResults.length} results similar to document '{docId}' found in {time}ms</Typography>
            </div>
            {searchResults.map((result: SearchResult) => <ResultCard key={result.url} searchResult={result} />)}
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

const SimilarDocuments = (): ReactElement => {
    const classes = useStyles();
    let { docId } = useParams();

    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [searchResults, setSearchResults] = useState<SearchResults | null>(null);

    useEffect(() => {
        console.log(docId);
        if (docId) {
            const docIdNb = parseInt(docId, 10);
            if (isNaN(docIdNb)) {
                setError(`Invalid docId '${docId}'`);
                setSearchResults(null);
            } else {
                handleSearch(docIdNb);
            }
        }
    }, [docId]);

    const handleSearch = async (docId: number): Promise<void> => {
        setSearchResults(null);
        setError(null);
        setIsLoading(true);

        try {
            const data: SearchResults = await getSimilarDocuments(docId);
            setSearchResults(data);
        } catch (error) {
            setError(`Error while searching for docId '${docId}'`);
            console.error(error);
        }

        setIsLoading(false);
    };

    return (
        <div style={{ marginTop: 30, marginBottom: 100 }}>
            <div className={classes.back}>
                <Link href="/" variant="body2">Back to search</Link>
            </div>

            {isLoading && <Spinner />}

            {searchResults && searchResults.results.length === 0 && !isLoading && !error && (
                <div className={classes.text}>
                    <Typography variant="caption">No results found for docId '{docId}'</Typography>
                </div>
            )}

            {!searchResults && !isLoading && error && (
                <div className={classes.text}>
                    <Typography variant="caption">{error}</Typography>
                </div>
            )}

            {searchResults && searchResults.results.length > 0 && docId && (
                <Results docId={docId} searchResults={searchResults.results} time={searchResults.time} />
            )}
        </div>
    );
};

export default SimilarDocuments;