#a custom version including all types in https://docs.ros.org/en/rolling/Concepts/Basic/About-Interfaces.html
bool	bool_value
byte	byte_value
char	char_value
float32	float32_value
float64	float64_value
int8	int8_value
uint8	uint8_value
int16	int16_value
uint16	uint16_value
int32	int32_value
uint32	uint32_value
int64	int64_value
uint64	uint64_value
string	string_value
wstring	wstring_value
int32[] unbounded_integer_array
int32[5] five_integers_array
int32[<=5] up_to_five_integers_array

string string_of_unbounded_size
string<=10 up_to_ten_characters_string

string[<=5] up_to_five_unbounded_strings
string<=10[] unbounded_array_of_strings_up_to_ten_characters_each
string<=10[<=5] up_to_five_strings_up_to_ten_characters_each