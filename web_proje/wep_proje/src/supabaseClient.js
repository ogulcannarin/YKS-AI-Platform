import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://vvhtryvswugtzlnocqdi.supabase.co'
const supabaseKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZ2aHRyeXZzd3VndHpsbm9jcWRpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY2OTI1ODUsImV4cCI6MjA5MjI2ODU4NX0.HOk6TY9Y9oitTqZRgZz6fa3KhA8du_nN70XwGXc6SY0'

export const supabase = createClient(supabaseUrl, supabaseKey)
