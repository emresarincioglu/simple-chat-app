package com.example.simplechat.core.network

internal object NetworkConstraints {
    // Database Fields
    const val FIELD_EMAIL = "email"
    const val FIELD_USER_NAME = "name"
    const val FIELD_AVATAR_PATH = "avatar_path"
    const val FIELD_PUBLIC_KEY = "public_key"
    const val FIELD_PRIVATE_KEY = "private_key"
    const val FIELD_IV = "iv"
    const val FIELD_FRIEND_REF = "ref"
    const val FIELD_IS_ACTIVE = "is_active"
    const val FIELD_MESSAGE_TEXT = "text"
    const val FIELD_MESSAGE_TIMESTAMP = "time"
    const val FIELD_MESSAGE_FROM_USER = "is_from_user"
    const val FIELD_LAST_MESSAGE_TIMESTAMP = "last_message_time"
    const val FIELD_MESSAGE_IMAGE_PATH = "image_path"

    // Database Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_FRIENDS = "friends"
    const val COLLECTION_MESSAGES = "messages"

    // Storage Folders
    const val FOLDER_AVATARS = "avatars"
    const val FOLDER_ENCRYPTED_IMAGES = "encrypted_images"
}