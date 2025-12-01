package com.example.educationapp.repository

import com.example.educationapp.data.Resource
import com.example.educationapp.data.dao.ResourceDao
import kotlinx.coroutines.flow.Flow

class ResourceRepository(private val resourceDao: ResourceDao) {
    fun getResourcesBySubjectAndDifficulty(subject: String, difficulty: String): Flow<List<Resource>> {
        return resourceDao.getResourcesBySubjectAndDifficulty(subject, difficulty)
    }

    fun getRecommendedResources(): Flow<List<Resource>> {
        return resourceDao.getRecommendedResources()
    }

    fun getResourcesByType(type: String): Flow<List<Resource>> {
        return resourceDao.getResourcesByType(type)
    }

    fun getAllResources(): Flow<List<Resource>> {
        return resourceDao.getAllResources()
    }

    suspend fun getResourceById(resourceId: Long): Resource? {
        return resourceDao.getResourceById(resourceId)
    }

    suspend fun insertResource(resource: Resource): Long {
        return resourceDao.insertResource(resource)
    }

    suspend fun updateResource(resource: Resource) {
        resourceDao.updateResource(resource)
    }

    suspend fun deleteResource(resource: Resource) {
        resourceDao.deleteResource(resource)
    }
}
