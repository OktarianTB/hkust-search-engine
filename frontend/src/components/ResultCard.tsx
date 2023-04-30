import React from 'react';
import {
    Card,
    CardHeader,
    CardContent,
    createStyles,
    makeStyles,
    Theme,
    Typography,
} from '@material-ui/core';
import { Container, Link } from '@mui/material';
import { Link as RouterLink } from "react-router-dom";
import { SearchResult } from '../types/ResultType';

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        score: {
            backgroundColor: theme.palette.primary.main,
            color: theme.palette.common.white,
            padding: theme.spacing(1),
            borderRadius: theme.shape.borderRadius,
        },
        header: {
            display: 'flex',
            flexDirection: 'column',
        },
        title: {
            display: 'flex',
        },
        properties: {
            marginBottom: theme.spacing(1),
            display: 'flex',
            justifyContent: 'space-between',
        },
        linkContainer: {
            marginBottom: theme.spacing(1),
        },
        links: {
            display: 'flex',
            flexDirection: 'column',
        },
        frequencies: {
            marginBottom: theme.spacing(1),
        },
        frequency: {
            marginRight: theme.spacing(1),
        }
    })
);

interface ResultCardProps {
    searchResult: SearchResult;
    index: number;
}

const ResultCard = ({ searchResult, index }: ResultCardProps) => {
    const { docId, score, url, properties, wordFrequencyMap, parentLinks, childLinks } = searchResult;
    const classes = useStyles();

    return (
        <Container sx={{ mt: 3, width: 1000 }}>
            <Card>
                <CardHeader
                    avatar={
                        <Typography variant="body1" className={classes.score}>
                            {score.toFixed(3)}
                        </Typography>
                    }
                    title={
                        <div className={classes.header}>
                            <div className={classes.title}>
                                <Typography variant="h6">#{index + 1} -&nbsp;</Typography>
                                <Link target="_blank" href={url} variant="h6" underline="none">{properties.title}</Link>
                            </div>
                            <Link target="_blank" href={url} variant="caption">{url}</Link>
                        </div>
                    }
                />
                <CardContent>
                    <div className={classes.properties}>
                        <Typography variant="subtitle2">DocId: {docId}</Typography>
                        <Typography variant="subtitle2">Page size: {properties.size}</Typography>
                        <Typography variant="subtitle2">Last modified: {properties.lastModifiedAt}</Typography>
                    </div>
                    <div className={classes.frequencies}>
                        <Typography variant="subtitle2">Top word frequencies:</Typography>
                        <div>
                            {
                                Object.entries(wordFrequencyMap).map(
                                    ([word, frequency]) => (
                                        <Typography key={word} variant="caption" className={classes.frequency}>
                                            {word}: {frequency};
                                        </Typography>
                                    )
                                )
                            }
                        </div>
                    </div>
                    <div className={classes.linkContainer}>
                        <Typography variant="subtitle2">Parent links:</Typography>
                        <div className={classes.links}>
                            {parentLinks.map((link) => <Typography key={link} variant="caption">- {link}</Typography>)}
                        </div>
                    </div>
                    <div className={classes.linkContainer}>
                        <Typography variant="subtitle2">Child links:</Typography>
                        <div className={classes.links}>
                            {childLinks.map((link) => <Typography key={link} variant="caption">- {link}</Typography>)}
                        </div>
                    </div>
                    <div>
                        <Link component={RouterLink} to={`/similar-documents/${docId}`} variant="caption">Search for similar documents</Link>
                    </div>
                </CardContent>
            </Card>
        </Container>
    );
};

export default ResultCard;