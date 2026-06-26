import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchCatalog } from '../store';

export function useBootstrapCatalog() {
  const dispatch = useDispatch();
  const catalogStatus = useSelector((state) => state.catalog.status);

  useEffect(() => {
    if (catalogStatus === 'idle') {
      dispatch(fetchCatalog());
    }
  }, [catalogStatus, dispatch]);
}
