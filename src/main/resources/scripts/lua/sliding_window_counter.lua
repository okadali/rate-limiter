-- KEYS[1]: Kullanıcıya/IP'ye ait temel anahtar (Örn: rate_limit:sliding_counter:192.168.1.1)
-- ARGV[1]: Limit (Kapasite - Örn: 10)
-- ARGV[2]: Pencere süresi (Saniye cinsinden - Örn: 60)
-- ARGV[3]: Şu anki zaman (Milisaniye cinsinden Java'dan gelir)

local base_key = KEYS[1]
local limit = tonumber(ARGV[1])
local window_size_sec = tonumber(ARGV[2])
local current_time_ms = tonumber(ARGV[3])

-- 1. ADIM: Şu anki ve bir önceki zaman pencerelerinin (örneğin dakikaların) numarasını bul
local current_window_id = math.floor(current_time_ms / (window_size_sec * 1000))
local previous_window_id = current_window_id - 1

-- 2. ADIM: Bu pencereler için Redis anahtarlarını oluştur
local current_key = base_key .. ':' .. current_window_id
local previous_key = base_key .. ':' .. previous_window_id

-- 3. ADIM: Şu anki pencerenin (dakikanın) yüzde kaçındayız?
local window_start_ms = current_window_id * window_size_sec * 1000
local elapsed_ms = current_time_ms - window_start_ms
local elapsed_sec = elapsed_ms / 1000.0

-- 4. ADIM: Önceki pencerenin ağırlık yüzdesini hesapla (Ne kadar zaman geçtiyse, önceki o kadar önemsizleşir)
local previous_weight = 1.0 - (elapsed_sec / window_size_sec)

-- 5. ADIM: Redis'ten mevcut ve önceki sayaçları al (O(1) hızında)
local current_count = tonumber(redis.call('GET', current_key) or '0')
local previous_count = tonumber(redis.call('GET', previous_key) or '0')

-- 6. ADIM: Cloudflare Algoritması Formülü (Tahmini mevcut yük)
local estimated_total_count = (previous_count * previous_weight) + current_count

-- Limit kontrolü
if estimated_total_count >= limit then
    return 0 -- Limit aşıldı!
end

-- Limiti aşmadıysa mevcut pencere sayacını 1 artır
redis.call('INCR', current_key)

-- Bellek (Memory) Sızıntısını Önlemek İçin:
-- Mevcut pencere sayacının ömrünü (TTL), pencere süresinin 2 katı yapıyoruz.
-- Çünkü bu sayaç, bir sonraki döngüde "previous_key" olarak bize lazım olacak, ondan sonra silinebilir.
redis.call('EXPIRE', current_key, window_size_sec * 2)

return 1 -- İzin verildi