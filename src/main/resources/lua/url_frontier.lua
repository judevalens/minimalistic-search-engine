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

local function dequeue_url(keys, args)
    local crawl_hash_session = keys[2]
    local host_names_set_key = keys[1]
    local target_ts = tonumber(args[1])
    local res = redis.call("ZRANGE", host_names_set_key, target_ts, target_ts, "BYSCORE", "LIMIT", target_ts, 1)
    if (#res == 0) then
        return nil
    end
end

redis.register_function("queue_url", queue_url)
redis.register_function("dequeue_url", queue_url)