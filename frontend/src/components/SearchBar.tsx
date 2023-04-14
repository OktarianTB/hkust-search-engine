import React, { useState } from 'react';
import { Button, Container, InputAdornment, Paper, TextField } from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";

interface Props {
    onSearch: (query: string) => void;
}

const SearchBar = ({ onSearch }: Props) => {
    const [searchQuery, setSearchQuery] = useState("");

    const handleChange = (event: { target: { value: React.SetStateAction<string>; }; }) => {
        setSearchQuery(event.target.value);
    };

    const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
        if (event.key === 'Enter') {
            event.preventDefault();
            onSearch(searchQuery);
        }
    }

    const handleClick = () => {
        onSearch(searchQuery);
    }

    return (
        <Container sx={{ mt: 8, mb: 8, width: 1000 }}>
            <Paper sx={{
                display: 'flex',
                alignItems: 'center',
                padding: 5,
            }}>
                <TextField
                    id="search"
                    type="search"
                    label="Search"
                    value={searchQuery}
                    onChange={handleChange}
                    onKeyDown={handleKeyDown}
                    sx={{ width: 1000 }}
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <SearchIcon />
                            </InputAdornment>
                        ),
                    }}
                />
                <Button
                    variant="outlined"
                    style={{ height: 56, marginLeft: 20 }}
                    onClick={handleClick}
                >
                    Search
                </Button>
            </Paper>
        </Container>
    );
}

export default SearchBar;