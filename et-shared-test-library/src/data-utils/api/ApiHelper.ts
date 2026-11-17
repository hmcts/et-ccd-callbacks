/**
 * Helper module for making HTTP requests using Axios.
 * Provides a wrapper for Axios requests with error handling and status validation.
 */
import axios, {AxiosRequestConfig, AxiosResponse} from 'axios';

const axiosClient = axios.create({});

/**
 * Makes an HTTP request using Axios and returns the response.
 * Throws an error if the response status is not 200 or 201, or if the request fails.
 *
 * @template T - The expected response data type.
 * @param requestParams - Axios request configuration object.
 * @returns Promise resolving to the Axios response.
 * @throws Error if the request fails or the response status is not 200 or 201.
 */
export async function axiosRequest<T = any>(
  requestParams: AxiosRequestConfig
): Promise<AxiosResponse<T>> {
  try {
    const response = await axiosClient(requestParams);
    if (![200, 201].includes(response.status)) {
      throw new Error(`Request to ${requestParams.url} failed with status ${response.status}. Response data: ${JSON.stringify(response.data)}`);
    }
    return response;
  } catch (error: any) {
    const serverMessage = error.response?.data
      ? ` \nServer response: ${JSON.stringify(error.response.data)}`
      : '';
    throw new Error(
      `Request to ${requestParams.url} failed: ${error.message}${serverMessage}`
    );
  }
}
