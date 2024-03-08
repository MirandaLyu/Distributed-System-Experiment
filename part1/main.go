package main

import (
	"net/http"
	"github.com/google/uuid"
	"github.com/gin-gonic/gin"
	"strconv"
	"sync"
)

// Album represents the data structure for an album.
type Album struct {
	Artist string `json:"artist"`
	Title  string `json:"title"`
	Year   string `json:"year"`
}

// ImageMetaData represents the data structure for image metadata.
type ImageMetaData struct {
	AlbumID  string `json:"albumID"`
	ImageSize string `json:"imageSize"`
}

// ErrorMsg represents the data structure for error messages.
type ErrorMsg struct {
	Msg string `json:"msg"`
}

// Albums map to store album data. For simplicity, using a map as an in-memory store.
// var Albums = make(map[string]Album)

// var (
// 	Albums     sync.Map
// )

// Albums struct to store album data with a mutex.
type Albums struct {
	sync.Mutex
	data map[string]Album
}

var albumsData = &Albums{
	data: make(map[string]Album),
}

func main() {
	router := gin.Default()

	// POST /albums Endpoint
	router.POST("/albums", func(c *gin.Context) {
		// Handle file upload first
		file, err := c.FormFile("image")
		if err != nil {
			c.JSON(http.StatusBadRequest, ErrorMsg{Msg: "Error uploading image"})
			return
		}
		
		// Get the image size in bytes and store in string
		fileSize := file.Size
		fileSizeString := strconv.FormatInt(fileSize, 10) + " bytes"// Use base 10 for integer conversion

		// Now, handle other form fields
		var formData struct {
			Profile struct {
				Artist string `form:"artist" binding:"required"`
				Title  string `form:"title" binding:"required"`
				Year   string `form:"year" binding:"required"`
			} `form:"profile" binding:"required"`
		}

		if err := c.ShouldBind(&formData); err != nil {
			c.JSON(http.StatusBadRequest, ErrorMsg{Msg: "Invalid request"})
			return
		}

		// Generate a unique albumID using UUID
		albumID := uuid.New().String()
		// Albums.Store(albumID, Album{
		// 	Artist: formData.Profile.Artist,
		// 	Title:  formData.Profile.Title,
		// 	Year:   formData.Profile.Year,
		// })

		// Lock the mutex before updating the data
		albumsData.Lock()
		defer albumsData.Unlock()

		albumsData.data[albumID] = Album{
			Artist: formData.Profile.Artist,
			Title:  formData.Profile.Title,
			Year:   formData.Profile.Year,
		}

		c.JSON(http.StatusOK, ImageMetaData{AlbumID: albumID, ImageSize: fileSizeString})
	})

	// GET /albums/{albumID} Endpoint
	router.GET("/albums/:albumID", func(c *gin.Context) {
		albumID := c.Param("albumID")
		// album, exists := Albums.Load(albumID)
		// if !exists {
		// 	c.JSON(http.StatusNotFound, ErrorMsg{Msg: "Key not found"})
		// 	return
		// }

		// Lock the mutex before reading the data
		albumsData.Lock()
		defer albumsData.Unlock()

		album, exists := albumsData.data[albumID]
		if !exists {
			c.JSON(http.StatusNotFound, ErrorMsg{Msg: "Key not found"})
			return
		}

		c.JSON(http.StatusOK, album)
	})

	// Run the server on port 8080
	router.Run(":8082")
}