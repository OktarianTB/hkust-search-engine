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
import { Result } from '../types/Result';

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        score: {
            backgroundColor: theme.palette.primary.main,
            color: theme.palette.common.white,
            padding: theme.spacing(1),
            borderRadius: theme.shape.borderRadius,
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

const CustomCard = ({
    title,
    score,
    url,
    size,
    lastModified,
    topWordFrequencies,
    parentLinks,
    childLinks,
}: Result) => {
    const classes = useStyles();

    return (
        <Container sx={{ mt: 3, width: 1000 }}>
            <Card>
                <CardHeader
                    avatar={
                        <Typography variant="body1" className={classes.score}>
                            {score}
                        </Typography>
                    }
                    title={
                        <div>
                            <Typography variant="h6">{title}</Typography>
                            <Link target="_blank" href={url} variant="caption">{url}</Link>
                        </div>
                    }
                />
                <CardContent>
                    <div className={classes.properties}>
                        <Typography variant="subtitle2">Page size: {size}</Typography>
                        <Typography variant="subtitle2">Last modified: {lastModified}</Typography>
                    </div>
                    <div className={classes.frequencies}>
                        <Typography variant="subtitle2">Top word frequencies:</Typography>
                        <div>
                            {topWordFrequencies.map((wordFrequency) => (
                                <Typography key={wordFrequency.word} variant="caption" className={classes.frequency}>
                                    {wordFrequency.word}: {wordFrequency.frequency};
                                </Typography>
                            ))}
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
                </CardContent>
            </Card>
        </Container>
    );
};

export default CustomCard;