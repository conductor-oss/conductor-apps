# Documentation and Code Search using Conductor
 
## Overview
A comprehensive system that indexes and enables semantic search across documentation and code repositories. The system consists of two main components:
1. **Indexing Pipeline**: Automatically processes and indexes documentation and code
2. **Search System**: Provides intelligent, context-aware search across all indexed content

## Setup / Requirements
1. Conductor developer account --> https://developer.orkescloud.com/
2. Create application keys/secret 
```shell

```
## Content Indexing

#### Documentation Indexer (`index_site`)
Processes website documentation through:
1. Sitemap crawling (`https://orkes.io/content/sitemap.xml`)
2. Content chunking (8000 chars, 200 char overlap)
3. Vector embedding generation
4. Storage in Pinecone ("orkesiodocs-pg-qa" namespace)

#### Code Repository Indexer (`github_repo_index`)
Indexes GitHub repositories by:
1. Loading repository content
2. Processing files iteratively
3. Maintaining version history (last 5 versions)
4. Storing in Pinecone ("conductor-sdk" namespace)

### 2. Search System (`site-search`)

#### Search Process
1. **Parallel Search Execution**:
  - Documentation search in "orkesiodocs-pg-qa" namespace
  - Code search in "conductor-sdk" namespace
  
2. **Intelligent Processing**:
  - Combines results from both sources
  - Validates context relevance using GPT-4
  - Generates comprehensive answers

3. **Performance Optimization**:
  - Caches results for 10 minutes
  - Parallel processing of search requests

## Technical Architecture

### Vector Database (Pinecone)
- **Indexes**: 
 - "orkesdocs" (code repository)
 - "test" (documentation)
- **Namespaces**:
 - "orkesiodocs-pg-qa": Documentation content
 - "conductor-sdk": Code repository content

### AI Models
- **Embedding**: OpenAI text-embedding-3-small
- **Context Understanding**: GPT-4
- **Answer Generation**: GPT-4

### Processing Configuration
- **Documentation Chunks**:
 - Size: 8000 characters
 - Overlap: 200 characters
- **Caching**:
 - TTL: 600 seconds (10 minutes)
 - Key: Query-based

## Workflows

### Indexing Workflows

#### 1. Site Documentation Indexer
- Inputs: None (fixed sitemap URL)
- Process: Parallel page processing
- Output: Vector embeddings in Pinecone

#### 2. GitHub Repository Indexer
- Inputs: 
 - `repo_url`: Repository URL
 - `namespace`: Target namespace
- Process: Iterative file processing
- Output: Vector embeddings with version history

### Search Workflow
- Input: `query` (search string)
- Process:
 1. Parallel vector search
 2. Result combination
 3. Relevance validation
 4. Answer generation
- Output: Contextual answer based on found content

## Key Features

### Indexing Features
- Automated content discovery
- Version control for code
- Parallel processing
- Context preservation
- Scalable architecture

### Search Features
- Dual-source search
- Smart result caching
- Context validation
- Intelligent answer generation
- Parallel query processing

## Benefits
1. **Comprehensive Coverage**: 
  - Documentation and code searchable in one system
  - Context-aware results

2. **Intelligent Processing**:
  - Semantic understanding
  - Relevance validation
  - Smart answer generation

3. **Performance**:
  - Parallel processing
  - Result caching
  - Efficient chunking

4. **Maintenance**:
  - Automated indexing
  - Version tracking
  - Regular updates

## Usage Notes
1. **System Requirements**:
  - Pinecone vector database
  - OpenAI API access
  - GitHub repository access

2. **Monitoring**:
  - Vector database usage
  - API rate limits
  - Cache performance
  - Index freshness

3. **Best Practices**:
  - Regular index updates
  - Cache TTL adjustment
  - Query optimization
  - Content chunk size tuning

## Error Handling
- Failed indexing retry mechanisms
- Cache fallback for search
- Permissive task execution
- Version conflict resolution

## Scalability
- Parallel processing capabilities
- Distributed search execution
- Configurable chunk sizes
- Flexible namespace organization