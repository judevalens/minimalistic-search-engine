#!lua name=spider
local function queue_url(keys, args)
    local serializedJob = args[1]
    local crawl_zset_session = keys[1]
    local crawl_hash_session = keys[2]
    local job = cmsgpack.unpack(serializedJob)
    if job == nil or type(job) ~= type(table) then
        return redis.error_reply("failed to unpack data")
    end
    redis.log(redis.LOG_DEBUG, job.normalized_url)
    local score = redis.call("ZSCORE", crawl_zset_session, job.normalized_url)
    redis.log(redis.LOG_DEBUG, tostring(score))
    if score ~= false then
        return nil
    end
    redis.log(redis.LOG_DEBUG, "queuing url job")
    local res = redis.call("ZADD", crawl_zset_session, job.score, job.normalized_url)
    if res == false then
        return nil
    end
    res = redis.call("HSET", crawl_hash_session, job.normalized_url, serializedJob)
    if res == false then
        return nil
    end
    return res
end

local function queue_crawling_job(keys, args)
    redis.setresp(3)
    redis.log(redis.LOG_DEBUG, "executing queueing job")
    local hostnames_zset_key = keys[1]
    local crawled_urls_set = keys[2]
    local session_key_prefix = keys[3]
    local serializedJob = args[1]
    local job = cmsgpack.unpack(serializedJob)
    local hostname_crawling_jobs_zset_hash_key = "crawling_jobs_zset_key"
    local hostname_urls_set_hash_key = "urls_set_key"
    if job == nil or type(job) ~= type(table) then
        return redis.error_reply("failed to unpack data")
    end
    -- hostname hash key is "crawling session" + actual hostname + "hash_key" suffix
    local hostname_hash_key = session_key_prefix .. job.hostname .. "_hash_key"
    local hostname_hash = redis.call("HMGET", hostname_hash_key, hostname_crawling_jobs_zset_hash_key, hostname_urls_set_hash_key)
    redis.log(redis.LOG_DEBUG, type(hostname_hash) .. "size: " .. tostring(#hostname_hash).." val 1: "..tostring(hostname_hash[1]).. " val2: ".. tostring(hostname_hash[2]))
    if (hostname_hash[1] == nil or hostname_hash[2] == nil) then
        redis.log(redis.LOG_DEBUG, "creating hash")
        local hostname_crawling_jobs_zset_key = session_key_prefix .. "_" .. job.hostname .. "_crawling_jobs_zset"
        local hostname_urls_set_key = session_key_prefix .. "_" .. job.hostname .. "_urls_set"
        local n_added_hash = redis.call("HSET",
                hostname_hash_key,
                hostname_crawling_jobs_zset_hash_key,
                hostname_crawling_jobs_zset_key,
                hostname_urls_set_hash_key,
                hostname_urls_set_key)
        if n_added_hash == 0 then
            return 0
        end
        hostname_hash = redis.call("HMGET", hostname_hash_key, hostname_crawling_jobs_zset_hash_key, hostname_urls_set_hash_key)
    end

    local isUrlVisited = redis.call("SISMEMBER", crawled_urls_set, job.normalized_url)
    if isUrlVisited == 1 then
        redis.log(redis.LOG_DEBUG, "already visited url")
        return 0
    end
    local hostname_urls_set_key = hostname_hash[2]
    local isUrlQueued = redis.call("SISMEMBER", hostname_urls_set_key, job.normalized_url)

    redis.log(redis.LOG_DEBUG, "got hash key\n" .. tostring(hostname_hash))
    local hostname_crawling_jobs_zset_key = hostname_hash[1]
    local n_updated_field = redis.call("ZADD", hostnames_zset_key, "CH", job.score, job.hostname)
    if n_updated_field == 0 then
        return 0
    end
    redis.log(redis.LOG_DEBUG, "hostname_crawling_jobs_zset_key: \n" .. tostring(hostname_crawling_jobs_zset_key))
    n_updated_field = redis.call("ZADD", hostname_crawling_jobs_zset_key, 0, serializedJob)
    if n_updated_field == 0 then
        return 0
    end

    n_updated_field = redis.call("SADD", hostname_urls_set_key, job.normalized_url)
    if n_updated_field == 0 then
        return 0
    end
    return 1
end

local function dequeue_crawling_job(keys, args)
    local host_names_zset_key = keys[1]
    local target_ts = tonumber(args[1])
    local res = redis.call("ZRANGE", host_names_zset_key, target_ts, target_ts, "BYSCORE", "LIMIT", target_ts, 1)
    if (#res == 0) then
        return nil
    end
end

redis.register_function("queue_crawling_job", queue_crawling_job)
redis.register_function("dequeue_crawling_job", dequeue_crawling_job)