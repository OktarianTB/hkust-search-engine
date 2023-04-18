import React from "react";
import { Box, CssBaseline } from "@mui/material";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import Search from "./pages/Search";
import NotFound from "./pages/NotFound";
import SimilarDocuments from "./pages/SimilarDocuments";

function App() {
  const theme = createTheme({
    palette: {
      background: {
        default: '#EEF1FF',
      },
      primary: {
        light: "#63b8ff",
        main: "#0989e3",
        dark: "#005db0",
        contrastText: "#000",
      },
      secondary: {
        main: "#4db6ac",
        light: "#82e9de",
        dark: "#00867d",
        contrastText: "#000",
      },
    },
  });

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box>
        <Router>
          <Routes>
            <Route path="/similar-documents/:docId" element={<SimilarDocuments />} />
            <Route path="/" element={<Search />} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </Router>
      </Box>
    </ThemeProvider>
  );
}

export default App;