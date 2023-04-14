import React, { ReactElement } from "react";
import SearchBar from "../components/SearchBar";
import Result from "../components/Result";
import { Typography } from "@mui/material";

const Search = (): ReactElement => {
    return (
        <div style={{ marginBottom: 100 }}>
            <SearchBar onSearch={function (query: string): void {
                console.log(query);
            }} />
            <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
            }}>
                <Typography variant="caption">2 results found</Typography>
            </div>
            <Result
                title={"card title"}
                score={0.54}
                url={"https://example.com"}
                size={"1234"}
                lastModified={"14/04/2023"}
                topWordFrequencies={[
                    { word: "hello", frequency: 5 },
                    { word: "wow", frequency: 4 },
                    { word: "hkust", frequency: 4 },
                    { word: "computer", frequency: 3 },
                    { word: "academics", frequency: 2 },
                ]}
                childLinks={["hello.com", "wow.com"]}
                parentLinks={["parent.com", "parent2.com"]}
            />
            <Result
                title={"card title"}
                score={0.54}
                url={"https://example.com"}
                size={"1234"}
                lastModified={"14/04/2023"}
                topWordFrequencies={[
                    { word: "hello", frequency: 5 },
                    { word: "wow", frequency: 4 },
                    { word: "hkust", frequency: 4 },
                    { word: "computer", frequency: 3 },
                    { word: "academics", frequency: 2 },
                ]}
                childLinks={["hello.com", "wow.com"]}
                parentLinks={["parent.com", "parent2.com"]}
            />
        </div>
    );
};

export default Search;